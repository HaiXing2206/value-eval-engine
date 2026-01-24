import { http } from './http'

export const caliberApi = {
  create: (caliberCode, weightVersionCode, remark) =>
    http.post('/api/v1/caliber', null, { params: { caliberCode, weightVersionCode, remark } }),
  get: (caliberCode) => http.get(`/api/v1/caliber/${caliberCode}`),
  publish: (caliberCode) => http.post(`/api/v1/caliber/${caliberCode}/publish`),
  copy: (caliberCode, newCaliberCode, remark) =>
    http.post(`/api/v1/caliber/${caliberCode}/copy`, null, { params: { newCaliberCode, remark } }),
  patch: (caliberCode, body) => http.patch(`/api/v1/caliber/${caliberCode}`, body)
}
