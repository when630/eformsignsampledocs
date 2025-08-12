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
  const res = await fetch(`${BASE_URL}/documents/tree`);
  if (!res.ok) throw new Error('카테고리 트리 로드 실패');
  return await res.json();
};

export const getDocumentsByCategory = async (categoryId: number) => {
  const response = await axios.get(`${BASE_URL}/documents/by-category/${categoryId}`);
  return response.data;
};

export const getCategoryPath = async (categoryId: number) => {
  const response = await axios.get(`${BASE_URL}/category/path/${categoryId}`);
  return response.data;
};

export const refreshToken = async (): Promise<any> => {
  const refreshToken = localStorage.getItem("refresh_token");
  if (!refreshToken) {
    throw new Error("로컬스토리지에 refresh_token이 없습니다.");
  }

  const res = await axios.post(
    "http://localhost:8080/api/auth/refresh",
    {},
    {
      headers: {
        Authorization: `Bearer ${refreshToken}`,
      },
    }
  );
  return res.data;
};

export async function searchFormsExact(params: {
  q: string;
  mode?: 'EQUAL' | 'WORD';
  page?: number;
  size?: number;
}) {
  const { q, mode = 'WORD', page = 0, size = 12 } = params;
  const url = new URL(`${BASE_URL}/forms/search-title-exact`);
  url.searchParams.set('q', q);
  url.searchParams.set('mode', mode);
  url.searchParams.set('page', String(page));
  url.searchParams.set('size', String(size));

  const res = await fetch(url.toString());
  if (!res.ok) throw new Error('검색 실패');
  return res.json(); // Spring Page<Document>
}

export async function getDocument(id: number) {
  const res = await fetch(`${BASE_URL}/documents/${id}`);
  if (!res.ok) throw new Error('문서 조회 실패');
  return res.json(); // { id, title, ... }
}

export async function getDocumentById(id: number) {
  const res = await fetch(`http://localhost:8080/api/documents/${id}`);
  if (!res.ok) throw new Error('문서 조회 실패');
  return res.json(); // 서버의 Document JSON (id, title, storageId, etc.)
}