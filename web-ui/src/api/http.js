import axios from 'axios'

export const http = axios.create({
  baseURL: '',
  timeout: 15000
})

http.interceptors.response.use(
  (resp) => resp.data,
  (err) => {
    const msg = err?.response?.data?.msg || err.message || '请求失败'
    return Promise.reject(new Error(msg))
  }
)
