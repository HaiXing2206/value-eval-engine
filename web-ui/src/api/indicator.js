import { http } from './http'

export const indicatorApi = {
  list: () => http.get('/api/v1/indicator'),
  create: (data) => http.post('/api/v1/indicator', data),
  update: (id, data) => http.put(`/api/v1/indicator/${id}`, data),
  disable: (id) => http.post(`/api/v1/indicator/${id}/disable`)
}
