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
  company_id: string;
}) => {
  const response = await axios.post(`${BASE_URL}/auth/register`, data);
  return response.data;
};

export const getCategoryTree = async () => {
  const res = await fetch('http://localhost:8080/api/documents/tree');
  if (!res.ok) throw new Error('카테고리 트리 로드 실패');
  return await res.json();
};

export const getDocumentsByCategory = async (categoryId: number) => {
  const response = await axios.get(`/api/documents/by-category/${categoryId}`);
  return response.data;
};