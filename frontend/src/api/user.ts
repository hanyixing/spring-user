import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
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
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export const userApi = {
  getAll: () => api.get<ApiResponse<User[]>>('/users').then(res => res.data.data),
  
  getById: (id: number) => api.get<ApiResponse<User>>(`/users/${id}`).then(res => res.data.data),
  
  create: (data: UserDTO) => api.post<ApiResponse<User>>('/users/register', data).then(res => res.data.data),
  
  update: (id: number, data: UserDTO) => api.put<ApiResponse<User>>(`/users/${id}`, data).then(res => res.data.data),
  
  delete: (id: number) => api.delete<ApiResponse<void>>(`/users/${id}`).then(res => res.data)
}

export default api
