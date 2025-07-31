// src/pages/login/index.tsx
import React, { useState } from 'react';
import './style.css';
import { useNavigate } from 'react-router-dom';
import { login } from '../../services/api';

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await login(email, password);
      localStorage.setItem('access_token', res.access_token);
      setError('');
      navigate('/');
    } catch (err: any) {
      setError('이메일 또는 비밀번호가 잘못되었습니다.');
    }
  };

  return (
    <div className="login-wrapper">
      <form className="login-form" onSubmit={handleSubmit}>
        <h2 className="login-title">로그인</h2>

        <input
          className="login-input"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="이메일"
        />
        <input
          className="login-input"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="비밀번호"
        />

        {error && <p className="login-error">{error}</p>}

        <button className="login-button" type="submit">
          로그인
        </button>
        <p className="register-link" onClick={() => navigate('/register')}>
          회원가입
        </p>
      </form>
    </div>
  );
};

export default LoginPage;