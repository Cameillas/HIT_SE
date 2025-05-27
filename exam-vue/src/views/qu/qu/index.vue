<template>

  <div>

    <data-table ref="pagingTable" :options="options" :list-query="listQuery" @multi-actions="handleMultiAction">
      <template #filter-content>

        <el-row>
          <el-col :span="24">

            <el-select v-model="listQuery.params.quType" class="filter-item" clearable>
              <el-option v-for="item in quTypes" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>

            <el-select v-model="listQuery.params.level" class="filter-item" clearable>
              <el-option v-for="item in levels" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>

            <repo-select v-model="listQuery.params.repoIds" :multi="true" />

            <el-input v-model="listQuery.params.content" placeholder="题目内容" style="width: 200px" class="filter-item" />

          </el-col>
        </el-row>

      </template>

      <template #data-columns>

        <el-table-column label="题目类型" align="center" width="100px">
          <template v-slot="scope">
            {{ scope.row.quType | quTypeFilter() }}
          </template>
        </el-table-column>

        <el-table-column label="题目内容" show-overflow-tooltip>
          <template v-slot="scope">
            {{ scope.row.content }}
          </template>
        </el-table-column>

        <el-table-column label="题目难度" align="center" width="100px">
          <template v-slot="scope">
            {{ scope.row.level | hardTypeFilter() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180px" fixed="right">
          <template v-slot="scope">
            <el-button size="mini" type="primary"
              @click="$router.push({ name: 'UpdateQu', params: { id: scope.row.id } })">
              修改
            </el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
        <!-- <el-table-column label="创建时间" align="center" prop="createTime" width="180px" /> -->

      </template>

    </data-table>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="30%">

      <el-form label-position="left" label-width="100px">

        <el-form-item label="操作题库" prop="repoIds">
          <repo-select v-model="dialogRepos" :multi="true" />
        </el-form-item>

        <el-row>
          <el-button type="primary" @click="handlerRepoAction">保存</el-button>
        </el-row>

      </el-form>

    </el-dialog>

    <span style="display: inline-block; width: 49%; text-align: center; vertical-align: top;">
      <h2 style="display: inline-block; margin: 0;">题型统计</h2>
      <v-chart :options="typeChartOptions" class="pie-chart" autoresize style="display: inline-block;" />
    </span>
    <span style="display: inline-block; width: 49%; text-align: center; vertical-align: top;">
      <h2 style="display: inline-block; margin: 0;">难度统计</h2>
      <v-chart :options="levelChartOptions" class="pie-chart" autoresize style="display: inline-block;" />
    </span>


  </div>
</template>

<script>
// import Vue from 'vue'
import ECharts from 'vue-echarts/components/ECharts.vue'
import 'echarts/lib/chart/pie'
import 'echarts/lib/component/tooltip'
import 'echarts/lib/component/title'

// Vue.component('v-chart', ECharts)

import DataTable from '@/components/DataTable'
import RepoSelect from '@/components/RepoSelect'
import { batchAction } from '@/api/qu/repo'
// import { exportExcel, importExcel, importTemplate } from '@/api/qu/qu'
import { post, get } from '@/utils/request'
export default {
  name: 'QuList',
  components: { RepoSelect, DataTable, 'v-chart': ECharts },
  data() {
    return {

      dialogTitle: '加入题库',
      dialogVisible: false,
      importVisible: false,
      dialogRepos: [],
      dialogQuIds: [],
      dialogFlag: false,

      listQuery: {
        current: 1,
        size: 10,
        params: {
          content: '',
          quType: '',
          level: '',
          repoIds: []
        }
      },

      quTypes: [
        {
          value: 1,
          label: '单选题'
        },
        {
          value: 2,
          label: '多选题'
        },
        {
          value: 3,
          label: '判断题'
        }
      ],
      levels: [
        {
          value: 1,
          label: '简单'
        },
        {
          value: 2,
          label: '困难'
        }
      ],

      options: {

        // 可批量操作
        multi: true,

        // 批量操作列表
        multiActions: [
          {
            value: 'add-repo',
            label: '加入题库..'
          },
          {
            value: 'remove-repo',
            label: '从..题库移除'
          },
          {
            value: 'delete',
            label: '删除'
          }
        ],
        // 列表请求URL
        listUrl: '/exam/api/qu/qu/paging',
        // 删除请求URL
        deleteUrl: '/exam/api/qu/qu/delete',
        // 添加数据路由
        addRoute: 'AddQu'
      },
      typeChartOptions: {},
      levelChartOptions: {}
    }
  },
  created() {
    this.generateChartOptions()
  },
  methods: {

    handleMultiAction(obj) {
      if (obj.opt === 'add-repo') {
        this.dialogTitle = '加入题库'
        this.dialogFlag = false
      }

      if (obj.opt === 'remove-repo') {
        this.dialogTitle = '从题库移除'
        this.dialogFlag = true
      }

      this.dialogVisible = true
      this.dialogQuIds = obj.ids
    },
    async handleDelete(id) {
      await post('/exam/api/qu/qu/delete', { ids: [id] })
      this.$notify({
        title: '成功',
        message: '删除成功！',
        type: 'success',
        duration: 2000
      })
      this.$refs.pagingTable.getList()
    },

    handlerRepoAction() {
      const postForm = { repoIds: this.dialogRepos, quIds: this.dialogQuIds, remove: this.dialogFlag }

      batchAction(postForm).then(() => {
        this.$notify({
          title: '成功',
          message: '批量操作成功！',
          type: 'success',
          duration: 2000
        })

        this.dialogVisible = false
        this.$refs.pagingTable.getList()
      })
    },
    generateChartOptions() {
      // Simulate data for the pie chart
      get('/exam/api/qu/qu/queryall').then(res => {
        const typeCategories = ['单选题', '多选题', '判断题']
        const levelCategories = ['简单', '困难']
        const data = res.data
        const quTypeCount = {}
        const levelCount = {}

        data.forEach(item => {
          levelCount[item.level] = (levelCount[item.level] || 0) + 1
          quTypeCount[item.quType] = (quTypeCount[item.quType] || 0) + 1
        })

        // 输出饼状图输入数据格式
        const levelPieData = Object.entries(levelCount).map(([key, value]) => ({
          name: levelCategories[Number(key) - 1] || `未知等级${key}`,
          value
        }))

        const quTypePieData = Object.entries(quTypeCount).map(([key, value]) => ({
          name: typeCategories[Number(key) - 1] || `未知题型${key}`,
          value
        }))
        this.typeChartOptions = {
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left',
            data: typeCategories.map(item => item.name)
          },
          color: ['#409EFF', '#E6A23C', '#67C23A'],
          series: [
            {
              name: '题型分布',
              type: 'pie',
              radius: '55%',
              center: ['50%', '60%'],
              data: quTypePieData,
              label: {
                show: true,
                formatter: '{b}: {c} ({d}%)'
              },
              labelLine: {
                show: true
              }
            }
          ]
        }
        this.levelChartOptions = {
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left',
            data: levelCategories.map(item => item.name)
          },
          color: ['#409EFF', '#E6A23C'],
          series: [
            {
              name: 'Sales',
              type: 'pie',
              radius: '55%',
              center: ['50%', '60%'],
              data: levelPieData,
              label: {
                show: true,
                formatter: '{b}: {c} ({d}%)'
              },
              labelLine: {
                show: true
              }
            }
          ]
        }
      })
    }
  }
}
</script>
