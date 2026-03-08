import { http } from './http'

export const taskApi = {
  calculate: (body) => http.post('/api/v1/task/calculate', body),
  importFile: (file, caliberVersionCode, taskNamePrefix, mappingJson) => {
    const form = new FormData()
    form.append('file', file)
    return http.post('/api/v1/task/import-file', form, {
      params: { caliberVersionCode, taskNamePrefix, mappingJson },
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  getTask: (taskId) => http.get(`/api/v1/task/${taskId}`),
  getResult: (taskId) => http.get(`/api/v1/task/${taskId}/result`),
  exportJson: (taskId, type) =>
    http.get(`/api/v1/task/${taskId}/export`, { params: { type, format: 'json' } }),
  list: (params) => http.get('/api/v1/task', { params }),
  full: (taskId) => http.get(`/api/v1/task/${taskId}/full`)
}
