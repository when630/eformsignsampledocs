import React, { useState } from 'react';
import './style.css';
import { useNavigate } from 'react-router-dom';
import { register } from '../../services/api';

const RegisterPage = () => {
  const [email, setEmail] = useState('');
  const [name, setName] = useState('');
  const [password, setPassword] = useState('');
  const [apiKey, setApiKey] = useState('');
  const [secretKey, setSecretKey] = useState('');
  const [company_id, setCompany_id] = useState('');
  const navigate = useNavigate();

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await register({ email, name, password, apiKey, secretKey, company_id });
      alert('회원가입 성공!');
      navigate('/login');
    } catch (err: any) {
      alert('회원가입 실패: ' + err?.response?.data || err.message);
    }
  };

  return (
    <div className="register-wrapper">
      <form className="register-form" onSubmit={handleRegister}>
        <h2 className="register-title">회원가입</h2>
        <input
          className="register-input"
          type="email"
          placeholder="이메일"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          className="register-input"
          type="text"
          placeholder="이름"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
        <input
          className="register-input"
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <input
          className="register-input"
          type="text"
          placeholder="API Key"
          value={apiKey}
          onChange={(e) => setApiKey(e.target.value)}
          required
        />
        <input
          className="register-input"
          type="text"
          placeholder="Secret Key"
          value={secretKey}
          onChange={(e) => setSecretKey(e.target.value)}
          required
        />
        <input
          className="register-input"
          type="text"
          placeholder="Company ID"
          value={company_id}
          onChange={(e) => setCompany_id(e.target.value)}
          required
        />
        <button type="submit" className="register-button">회원가입</button>
      </form>
    </div>
  );
};

export default RegisterPage;