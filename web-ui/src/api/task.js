import { http } from './http'

export const taskApi = {
  calculate: (body) => http.post('/api/v1/task/calculate', body),
  getTask: (taskId) => http.get(`/api/v1/task/${taskId}`),
  getResult: (taskId) => http.get(`/api/v1/task/${taskId}/result`),
  exportJson: (taskId, type) =>
    http.get(`/api/v1/task/${taskId}/export`, { params: { type, format: 'json' } }),
  list: (params) => http.get('/api/v1/task', { params }),
  full: (taskId) => http.get(`/api/v1/task/${taskId}/full`)
}
