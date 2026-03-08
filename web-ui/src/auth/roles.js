export const ROLE_OPTIONS = [
  { label: '业务评估人员', value: 'evaluator' },
  { label: '审核/审计人员', value: 'auditor' },
  { label: '系统管理员', value: 'admin' },
  { label: '开发/运维人员', value: 'devops' }
]

export const ROLE_LABEL_MAP = ROLE_OPTIONS.reduce((acc, item) => {
  acc[item.value] = item.label
  return acc
}, {})

export const ROUTE_ROLE_ACCESS = {
  '/dashboard': ['evaluator', 'auditor', 'admin', 'devops'],
  '/indicators': ['evaluator', 'admin'],
  '/rules': ['auditor', 'admin'],
  '/weights': ['auditor', 'admin'],
  '/calibers': ['auditor', 'admin'],
  '/systems': ['auditor', 'admin'],
  '/tasks': ['evaluator', 'admin'],
  '/history': ['evaluator', 'auditor', 'admin', 'devops']
}

export const getDefaultPathByRole = (role) => {
  const allowed = Object.entries(ROUTE_ROLE_ACCESS)
    .filter(([, roles]) => roles.includes(role))
    .map(([path]) => path)
  return allowed[0] || '/dashboard'
}

export const canAccessPath = (role, path) => {
  const roles = ROUTE_ROLE_ACCESS[path]
  if (!roles) {
    return true
  }
  return roles.includes(role)
}
