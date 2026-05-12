import request from '../request'

export const userApi = {
  register(data) {
    return request.post('/users', data)
  },
  login(data) {
    return request.post('/users/sessions', data)
  },
  logout() {
    return request.delete('/users/sessions')
  },
  profile() {
    return request.get('/users/me')
  },
}
