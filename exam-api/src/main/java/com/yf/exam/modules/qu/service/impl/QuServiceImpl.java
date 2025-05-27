package com.yf.exam.modules.qu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yf.exam.core.api.dto.PagingReqDTO;
import com.yf.exam.core.exception.ServiceException;
import com.yf.exam.core.utils.BeanMapper;
import com.yf.exam.modules.qu.dto.QuAnswerDTO;
import com.yf.exam.modules.qu.dto.QuDTO;
import com.yf.exam.modules.qu.dto.ext.QuDetailDTO;
import com.yf.exam.modules.qu.dto.request.QuQueryReqDTO;
import com.yf.exam.modules.qu.entity.Qu;
import com.yf.exam.modules.qu.entity.QuAnswer;
import com.yf.exam.modules.qu.entity.QuRepo;
import com.yf.exam.modules.qu.enums.QuType;
import com.yf.exam.modules.qu.mapper.QuMapper;
import com.yf.exam.modules.qu.service.QuAnswerService;
import com.yf.exam.modules.qu.service.QuRepoService;
import com.yf.exam.modules.qu.service.QuService;
import com.yf.exam.modules.qu.utils.ImageCheckUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 语言设置 服务实现类
 * </p>
 *
 
 * @since 05-25 10:17
 */
@Service
public class QuServiceImpl extends ServiceImpl<QuMapper, Qu> implements QuService {

    @Autowired
    private QuAnswerService quAnswerService;

    @Autowired
    private QuRepoService quRepoService;

    @Autowired
    private ImageCheckUtils imageCheckUtils;

    @Override
    public IPage<QuDTO> paging(PagingReqDTO<QuQueryReqDTO> reqDTO) {

        //创建分页对象
        Page page = new Page<>(reqDTO.getCurrent(), reqDTO.getSize());

        //转换结果
        IPage<QuDTO> pageData = baseMapper.paging(page, reqDTO.getParams());
        return pageData;
    }

    @Override
    public List<QuDTO> queryall() {

        //转换结果
        List<QuDTO> Data = baseMapper.queryall();
        return Data;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<String> ids) {
        // 移除题目
        this.removeByIds(ids);

        // 移除选项
        QueryWrapper<QuAnswer> wrapper = new QueryWrapper<>();
        wrapper.lambda().in(QuAnswer::getQuId, ids);
        quAnswerService.remove(wrapper);

        // 移除题库绑定
        QueryWrapper<QuRepo> wrapper1 = new QueryWrapper<>();
        wrapper1.lambda().in(QuRepo::getQuId, ids);
        quRepoService.remove(wrapper1);
    }

    @Override
    public List<Qu> listByRandom(String repoId, Integer quType, List<String> excludes, Integer size) {
        return baseMapper.listByRandom(repoId, quType, excludes, size);
    }

    @Override
    public QuDetailDTO detail(String id) {

        QuDetailDTO respDTO = new QuDetailDTO();
        Qu qu = this.getById(id);
        BeanMapper.copy(qu, respDTO);

        List<QuAnswerDTO> answerList = quAnswerService.listByQu(id);
        respDTO.setAnswerList(answerList);

        List<String> repoIds = quRepoService.listByQu(id);
        respDTO.setRepoIds(repoIds);

        return respDTO;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(QuDetailDTO reqDTO) {


        // 校验数据
        this.checkData(reqDTO, "");

        Qu qu = new Qu();
        BeanMapper.copy(reqDTO, qu);

        // 校验图片地址
        imageCheckUtils.checkImage(qu.getImage(), "题干图片地址错误！");

        // 更新
        this.saveOrUpdate(qu);

        // 保存全部问题
        quAnswerService.saveAll(qu.getId(), reqDTO.getAnswerList());

        // 保存到题库
        quRepoService.saveAll(qu.getId(), qu.getQuType(), reqDTO.getRepoIds());

    }


    /**
     * 校验题目信息
     *
     * @param qu
     * @param no
     * @throws Exception
     */
    public void checkData(QuDetailDTO qu, String no) {


        if (StringUtils.isEmpty(qu.getContent())) {
            throw new ServiceException(1, no + "题目内容不能为空！");
        }


        if (CollectionUtils.isEmpty(qu.getRepoIds())) {
            throw new ServiceException(1, no + "至少要选择一个题库！");
        }

        List<QuAnswerDTO> answers = qu.getAnswerList();


            if (CollectionUtils.isEmpty(answers)) {
                throw new ServiceException(1, no + "客观题至少要包含一个备选答案！");
            }


            int trueCount = 0;
            for (QuAnswerDTO a : answers) {

                if (a.getIsRight() == null) {
                    throw new ServiceException(1, no + "必须定义选项是否正确项！");
                }

                if (StringUtils.isEmpty(a.getContent())) {
                    throw new ServiceException(1, no + "选项内容不为空！");
                }

                if (a.getIsRight()) {
                    trueCount += 1;
                }
            }

            if (trueCount == 0) {
                throw new ServiceException(1, no + "至少要包含一个正确项！");
            }


            //单选题
            if (qu.getQuType().equals(QuType.RADIO) && trueCount > 1) {
                throw new ServiceException(1, no + "单选题不能包含多个正确项！");
            }

    }
}
