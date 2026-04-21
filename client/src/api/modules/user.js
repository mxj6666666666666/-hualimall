import request from '../request'

export const userApi = {
  register(data) {
    return request.post('/users', data)
  },
  login(data) {
    return request.post('/users/sessions', data)
  },
  profile() {
    return request.get('/users/me')
  },
}
