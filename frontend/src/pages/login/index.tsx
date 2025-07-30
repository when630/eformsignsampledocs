import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './style.css'; // 스타일 파일 경로에 맞게 수정

import { login } from '../../services/api'; // 상대경로로 수정

const LoginPage = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await login(email, password);
      console.log('✅ 로그인 성공:', res);

      // 토큰 저장 (예: localStorage)
      localStorage.setItem('access_token', res.access_token);
      localStorage.setItem('refresh_token', res.refresh_token);

      // 메인 페이지로 이동
      navigate('/');
    } catch (err: any) {
      console.error('❌ 로그인 실패:', err);
      setErrorMessage(typeof err === 'string' ? err : '로그인 실패');
    }
  };

  return (
    <div className="login-wrapper">
      <form className="login-form" onSubmit={handleLogin}>
        <h2 className="login-title">로그인</h2>
        {errorMessage && <p className="login-error">{errorMessage}</p>}
        <input
          type="email"
          placeholder="이메일"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="login-input"
          required
        />
        <input
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="login-input"
          required
        />
        <button type="submit" className="login-button">
          로그인
        </button>
      </form>
    </div>
  );
};

export default LoginPage;