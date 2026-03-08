import { http } from './http'

export const weightApi = {
  listVersions: () => http.get('/api/v1/weight/version'),
  createVersion: (versionCode, systemVersionCode, remark) =>
    http.post('/api/v1/weight/version', null, { params: { versionCode, systemVersionCode, remark } }),
  getVersion: (versionCode) => http.get(`/api/v1/weight/version/${versionCode}`),
  listItems: (versionCode) => http.get(`/api/v1/weight/version/${versionCode}/items`),
  upsertItems: (versionCode, items) =>
    http.post(`/api/v1/weight/version/${versionCode}/items`, items),
  publish: (versionCode) => http.post(`/api/v1/weight/version/${versionCode}/publish`)
}
