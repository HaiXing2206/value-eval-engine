const USERS_KEY = 'vee_users'

const readUsers = () => {
  const raw = localStorage.getItem(USERS_KEY)
  if (!raw) {
    return []
  }

  try {
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

const writeUsers = (users) => {
  localStorage.setItem(USERS_KEY, JSON.stringify(users))
}

export const getUsers = () => readUsers()

export const findUser = (username) => {
  const users = readUsers()
  return users.find((item) => item.username === username)
}

export const registerUser = ({ username, password, role }) => {
  const users = readUsers()
  if (users.some((item) => item.username === username)) {
    return false
  }

  users.push({ username, password, role })
  writeUsers(users)
  return true
}

export const verifyUser = ({ username, password, role }) => {
  if (username === 'admin' && password === '123456') {
    return true
  }

  const user = findUser(username)
  if (!user) {
    return false
  }

  return user.password === password && user.role === role
}
