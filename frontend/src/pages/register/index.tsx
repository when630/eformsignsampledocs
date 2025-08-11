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
          className="register-info-input"
          type="email"
          placeholder="이메일"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          className="register-info-input"
          type="text"
          placeholder="이름"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
        <input
          className="register-info-input"
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <p className="register-caption">
          <b>※ API를 먼저 생성해주셔야 됩니다.</b><br/>ⓘ 커넥트 → API / Webhook → API 키 생성
        </p>
        <input
          className="register-input"
          type="text"
          placeholder="API Key"
          value={apiKey}
          onChange={(e) => setApiKey(e.target.value)}
          required
        />
        <p className="register-caption">
          ⓘ 커넥트 → API / Webhook → API 키
        </p>
        <input
          className="register-input"
          type="text"
          placeholder="Secret Key"
          value={secretKey}
          onChange={(e) => setSecretKey(e.target.value)}
          required
        />
        <p className="register-caption">
          ⓘ 커넥트 → API / Webhook → 키 보기 → 비밀 키
        </p>
        <input
          className="register-input"
          type="text"
          placeholder="Company ID"
          value={company_id}
          onChange={(e) => setCompany_id(e.target.value)}
          required
        />
        <p className="register-caption">
          ⓘ 회사 관리 → 회사 정보 → 회사 ID
        </p>

        {/* ─────────── 면책 고지 + 동의 섹션 ─────────── */}
        <section className="legal-card" aria-labelledby="legal-title">
          <h3 id="legal-title" className="legal-title">서식에 관한 안내</h3>
          <p className="legal-text">
            서식의 내용은 사용자가 수정, 보완, 삭제할 수 있으며, 이러한 변경이 이루어진 경우를 포함하여 해당 서식의 내용에 대하여 서식 제공자 및 이폼사인은 법적 효력을 보증하지 않습니다.<br/>
            서식을 그대로 사용하거나, 수정·보완·삭제하여 사용함으로써 발생하는 분쟁 또는 손해(제3자와의 분쟁 및 손해를 포함)에 대하여 서식 제공자와 이폼사인은 법적 책임을 지지 않습니다.<br/>
            사용자는 서식을 이폼사인 에디터 내에서만 사용할 수 있으며, 서식의 무단 유출, 배포 등 외부 사용은 엄격히 금지됩니다.
          </p>

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