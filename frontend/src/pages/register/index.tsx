import React, { useState, useEffect } from 'react';
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

  // 동의 체크
  const [agreed, setAgreed] = useState(false);
  const consentStorageKey = 'signup.legalConsent';
  const navigate = useNavigate();

  useEffect(() => {
    const saved = localStorage.getItem(consentStorageKey);
    if (saved === 'true') setAgreed(true);
  }, []);

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!agreed) {
      alert('면책 고지에 동의하셔야 회원가입이 가능합니다.');
      return;
    }
    try {
      await register({ email, name, password, apiKey, secretKey, company_id });
      alert('회원가입 성공!');
      navigate('/login');
    } catch (err: any) {
      alert('회원가입 실패: ' + (err?.response?.data || err.message));
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

        {/* ─────────── 면책 고지 + 동의 섹션 ─────────── */}
        <section className="legal-card" role="region" aria-labelledby="legal-title">
          <h3 id="legal-title" className="legal-title">샘플 양식 이용에 관한 면책 고지</h3>
          <p className="legal-text">
            본 페이지에서 제공되는 <b>샘플 양식</b>은 일반적인 참고 자료이며 <b>법률 자문이 아닙니다</b>. <br/>
            사용·수정·배포 과정에서 발생하는 결과와 책임은 전적으로 사용자에게 있으며, 당사는 양식의 <b>정확성·완전성·최신성</b>을 보증하지 않습니다. <br/>
            이를 이용함으로써 발생하는 <b>직접·간접·부수적 손해</b>에 대하여 당사는 어떠한 법적 책임도 부담하지 않습니다.<br/>
            필요 시 법률 전문가의 검토를 권장드립니다.
          </p>

          <details className="legal-details">
            <summary>자세히 보기</summary>
            <ul>
              <li>특정 상황이나 최신 법령에 부적합할 수 있습니다.</li>
              <li>항목의 추가/삭제/수정은 사용자 책임입니다.</li>
              <li>샘플 제공만으로 당사의 법적 의무는 발생하지 않습니다.</li>
            </ul>
          </details>

          <label className="legal-consent-row">
            <input
              type="checkbox"
              checked={agreed}
              onChange={(e) => {
                const v = e.target.checked;
                setAgreed(v);
                localStorage.setItem('signup.legalConsent', String(v));
                if (v) {
                  localStorage.setItem('signup.legalConsentAt', new Date().toISOString());
                } else {
                  localStorage.removeItem('signup.legalConsentAt');
                }
              }}
              aria-describedby="legal-title"
            />
            <span>위 내용을 모두 확인하였으며, <b>동의합니다</b>.</span>
          </label>
        </section>

        <button
          type="submit"
          className={`register-button ${!agreed ? 'is-disabled' : ''}`}
          disabled={!agreed}
          aria-disabled={!agreed}
          title={!agreed ? '면책 고지에 동의해야 회원가입이 가능합니다.' : '회원가입'}
        >
          회원가입
        </button>
      </form>
    </div>
  );
};

export default RegisterPage;