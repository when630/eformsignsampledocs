import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api';

export const login = async (email: string, password: string): Promise<any> => {
  try {
    const response = await axios.post(`${BASE_URL}/auth/login`, {
      email,
      password,
    });
    return response.data; // access_token, refresh_token 등
  } catch (error: unknown) {
    // 타입 단언 후 처리
    if (axios.isAxiosError(error) && error.response) {
      throw error.response.data;
    }
    throw new Error('로그인 실패');
  }
};

export const registerAccount = async ({ email, name, password }: { email: string; name: string; password: string }) => {
  try {
    const response = await axios.post(`${BASE_URL}/auth/register`, {
      email,
      name,
      password,
    });
    return response.data;
  } catch (error: any) {
    throw error.response?.data || '회원가입 실패';
  }
};