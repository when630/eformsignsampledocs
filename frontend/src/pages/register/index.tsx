// src/pages/register/index.tsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './style.css';
import { registerAccount } from '../../services/api';

const RegisterPage = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [name, setName] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await registerAccount({ email, name, password });
      alert('회원가입이 완료되었습니다.'); 
      navigate('/login');
    } catch (err: any) {
      setError(err.message || '회원가입 실패');
    }
  };

  return (
    <div className="register-wrapper">
      <form className="register-form" onSubmit={handleRegister}>
        <h2 className="register-title">회원가입</h2>
        {error && <div className="register-error">{error}</div>}
        <input
          type="email"
          placeholder="이메일"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="register-input"
          required
        />
        <input
          type="text"
          placeholder="이름"
          value={name}
          onChange={(e) => setName(e.target.value)}
          className="register-input"
          required
        />
        <input
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="register-input"
          required
        />
        <button type="submit" className="register-button">가입하기</button>
      </form>
    </div>
  );
};

export default RegisterPage;