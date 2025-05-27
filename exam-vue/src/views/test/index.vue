<template>
  <div class="pie-chart-container">
    <h2>Sales Distribution by Category</h2>
    <v-chart :options="chartOptions" class="pie-chart" autoresize />
  </div>
</template>

<script>
import ECharts from 'vue-echarts'
import 'echarts/lib/chart/pie' // Import the pie chart type
import 'echarts/lib/component/tooltip' // Import tooltip component
import 'echarts/lib/component/legend' // Import legend component

export default {
  name: 'PieChartExample',
  components: {
    'v-chart': ECharts
  },
  data() {
    return {
      chartOptions: {}
    }
  },
  created() {
    this.generateChartOptions()
  },
  methods: {
    generateChartOptions() {
      // Simulate data for the pie chart
      const categories = ['Electronics', 'Clothing', 'Home Goods', 'Books', 'Groceries']
      const salesData = categories.map(category => ({
        name: category,
        value: Math.floor(Math.random() * 500) + 100 // Random sales value between 100 and 600
      }))

      this.chartOptions = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          data: categories
        },
        series: [
          {
            name: 'Sales',
            type: 'pie',
            radius: '55%',
            center: ['50%', '60%'],
            data: salesData,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            },
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
    }
  }
}
</script>

<style scoped>
.pie-chart-container {
  width: 100%;
  max-width: 800px; /* Adjust as needed */
  margin: 20px auto;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  background-color: #fff;
  text-align: center;
}

h2 {
  color: #333;
  margin-bottom: 20px;
}

.pie-chart {
  width: 100%;
  height: 400px; /* Set a fixed height for the chart */
}
</style>
