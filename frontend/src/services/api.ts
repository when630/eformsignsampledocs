import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api';

export const login = async (email: string, password: string) => {
  try {
    const response = await axios.post(`${BASE_URL}/auth/login`, {
      email,
      password,
    });

    return response.data; // { access_token, email }
  } catch (error: any) {
    throw error.response?.data || '로그인 실패';
  }
};

export const register = async (data: {
  email: string;
  name: string;
  password: string;
  apiKey: string;
  secretKey: string;
}) => {
  const response = await axios.post(`${BASE_URL}/auth/register`, data);
  return response.data;
};