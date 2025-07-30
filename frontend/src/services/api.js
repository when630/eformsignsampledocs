import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api'; // 실제 서버 주소로

export const login = async (email, password) => {
  try {
    const response = await axios.post(`${BASE_URL}/auth/login`, {
      email,
      password,
    });

    return response.data; // access_token, refresh_token 등
  } catch (error) {
    throw error.response?.data || '로그인 실패';
  }
};