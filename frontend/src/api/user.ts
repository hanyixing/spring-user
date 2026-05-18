import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
})

export interface User {
  id: number
  username: string
  password?: string
  realName: string
  idCard: string
  phone: string
  email: string
  status?: number
  role?: string
  createTime?: string
  updateTime?: string
}

export interface UserDTO {
  username: string
  password: string
  realName: string
  idCard: string
  phone: string
  email: string
  role?: string
}

export interface LoginLog {
  id: number
  userId: number
  username: string
  loginTime: string
  ipAddress: string
  loginStatus: number
  message: string
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export const userApi = {
  login: (username: string, password: string) => 
    api.post<ApiResponse<User>>('/users/login', null, { params: { username, password } }).then(res => {
      if (res.data.code !== 200) {
        throw { response: { data: res.data } }
      }
      return res.data.data
    }),
  
  getAll: () => api.get<ApiResponse<User[]>>('/users').then(res => res.data.data),
  
  getActive: () => api.get<ApiResponse<User[]>>('/users/active').then(res => res.data.data),
  
  getById: (id: number) => api.get<ApiResponse<User>>(`/users/${id}`).then(res => res.data.data),
  
  create: (data: UserDTO) => api.post<ApiResponse<User>>('/users/register', data).then(res => res.data.data),
  
  update: (id: number, data: UserDTO) => api.put<ApiResponse<User>>(`/users/${id}`, data).then(res => res.data.data),
  
  delete: (id: number) => api.delete<ApiResponse<void>>(`/users/${id}`).then(res => res.data),
  
  updatePassword: (id: number, newPassword: string) => 
    api.put<ApiResponse<void>>(`/users/${id}/password`, null, { params: { newPassword } }).then(res => res.data),
  
  updateRole: (id: number, newRole: string) => 
    api.put<ApiResponse<void>>(`/users/${id}/role`, null, { params: { newRole } }).then(res => res.data),
  
  getLoginLogs: (id: number) => 
    api.get<ApiResponse<LoginLog[]>>(`/users/${id}/login-logs`).then(res => res.data.data),
  
  getAllLoginLogs: () => 
    api.get<ApiResponse<LoginLog[]>>('/users/login-logs').then(res => res.data.data)
}

export default api
