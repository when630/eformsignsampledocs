import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './style.css';
import { login } from '../../services/api'; // 상대경로 확인

const LoginPage = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg('');

    try {
      const result = await login(email, password);
      console.log('로그인 성공:', result);

      // 로컬스토리지에 토큰 저장
      localStorage.setItem('access_token', result.access_token);
      localStorage.setItem('refresh_token', result.refresh_token);

      navigate('/');
    } catch (err: any) {
      console.error('로그인 실패:', err);
      setErrorMsg(typeof err === 'string' ? err : '로그인에 실패했습니다.');
    }
  };

  return (
    <div className="login-wrapper">
      <form className="login-form" onSubmit={handleLogin}>
        <h2 className="login-title">로그인</h2>
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
        {errorMsg && <div className="login-error">{errorMsg}</div>}
        <button type="submit" className="login-button">
          로그인
        </button>
      </form>
    </div>
  );
};

export default LoginPage;