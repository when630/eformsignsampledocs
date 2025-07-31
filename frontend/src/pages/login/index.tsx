import React, { useState } from 'react';
import './style.css';
import { useNavigate } from 'react-router-dom';
import { login } from '../../services/api';

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await login(email, password);
      localStorage.setItem('access_token', res.access_token);
      alert('로그인 성공');
      navigate('/');
    } catch (err: any) {
      alert('로그인 실패: ' + err?.response?.data || err.message);
    }
  };

  return (
    <div className="login-wrapper">
      <form className="login-form" onSubmit={handleSubmit}>
        <h2 className="login-title">로그인</h2>
        <input
          className="login-input"
          type="email"
          placeholder="이메일"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          className="login-input"
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button type="submit" className="login-button">로그인</button>
        <p className="register-link" onClick={() => navigate('/register')}>회원가입</p>
      </form>
    </div>
  );
};

export default LoginPage;