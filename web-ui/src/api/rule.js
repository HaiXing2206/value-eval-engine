import { http } from './http'

export const ruleApi = {
  upsertQual: (code, items) => http.post(`/api/v1/rule/qual/${code}`, items),
  upsertQuant: (code, items) => http.post(`/api/v1/rule/quant/${code}`, items),
  upsertPreprocess: (code, body) => http.post(`/api/v1/rule/preprocess/quant/${code}`, body)
}
