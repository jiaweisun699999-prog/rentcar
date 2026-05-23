const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8081, // 前端启动端口
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端 Spring Boot 默认端口
        changeOrigin: true,
        // 如果后端接口没有 /api 前缀，可以取消注释下面这行进行重写
        // pathRewrite: { '^/api': '' }
      }
    }
  }
})
