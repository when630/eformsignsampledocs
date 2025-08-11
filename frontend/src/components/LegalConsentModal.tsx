import React, { useEffect, useMemo } from 'react';
import './LegalConsentModal.css'

const fmt = (iso?: string | null) => {
  if (!iso) return '';
  try {
    const d = new Date(iso);
    // 한국 시간 포맷
    return new Intl.DateTimeFormat('ko-KR', {
      year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit', second: '2-digit'
    }).format(d);
  } catch {
    return '';
  }
};

type Props = {
  open: boolean;
  onClose: () => void;
  storageKey?: string;        // 기본: 'signup.legalConsent'
  storageKeyAt?: string;      // 기본: 'signup.legalConsentAt'
};

const LegalConsentModalViewOnly: React.FC<Props> = ({
  open,
  onClose,
  storageKey = 'signup.legalConsent',
  storageKeyAt = 'signup.legalConsentAt',
}) => {
  const [agreed, setAgreed] = React.useState(false);
  const [agreedAt, setAgreedAt] = React.useState<string | null>(null);

  useEffect(() => {
    if (!open) return;
    const saved = localStorage.getItem(storageKey);
    setAgreed(saved === 'true');
    setAgreedAt(localStorage.getItem(storageKeyAt));
  }, [open, storageKey, storageKeyAt]);

  // 스크롤 잠금
  useEffect(() => {
    if (!open) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => { document.body.style.overflow = prev; };
  }, [open]);

  const statusLabel = useMemo(() => (agreed ? '동의 완료' : '동의하지 않음'), [agreed]);
  const timeLabel = useMemo(() => (agreed ? fmt(agreedAt) : ''), [agreed, agreedAt]);

  if (!open) return null;

  return (
    <div className="legal-modal-overlay" role="dialog" aria-modal="true" aria-labelledby="legal-modal-title">
      <div className="legal-modal">
        <div className="legal-modal-header">
          <div className="legal-modal-header-left">
            <h3 id="legal-modal-title" className="legal-modal-title">면책 고지</h3>
            <span className="status-badge ok">동의 완료</span>
            {timeLabel && <span className="status-time">동의 시각: {timeLabel}</span>}
          </div>
          <button className="legal-modal-close" onClick={onClose} aria-label="닫기">×</button>
        </div>

        <div className="legal-modal-body">

          <section className="legal-card" role="region" aria-labelledby="legal-title">
            <h3 id="legal-title" className="legal-title">샘플 양식 이용에 관한 면책 고지</h3>
            <p className="legal-text">
              본 페이지에서 제공되는 <b>샘플 양식</b>은 일반적인 참고 자료이며 <b>법률 자문이 아닙니다</b>. <br />
              사용·수정·배포 과정에서 발생하는 결과와 책임은 전적으로 사용자에게 있으며, 당사는 양식의 <b>정확성·완전성·최신성</b>을 보증하지 않습니다. <br />
              이를 이용함으로써 발생하는 <b>직접·간접·부수적 손해</b>에 대하여 당사는 어떠한 법적 책임도 부담하지 않습니다.<br />
              필요 시 법률 전문가의 검토를 권장드립니다.
            </p>

            <details className="legal-details">
              <summary>자세히 보기</summary>
                <p>- 특정 상황이나 최신 법령에 부적합할 수 있습니다.</p>
                <p>- 항목의 추가/삭제/수정은 사용자 책임입니다.</p>
                <p>- 샘플 제공만으로 당사의 법적 의무는 발생하지 않습니다.</p>
            </details>
          </section>

          {!agreed && (
            <p className="legal-hint">
              아직 회원가입 화면에서 동의하지 않으셨습니다. 회원가입 과정에서 동의하실 수 있습니다.
            </p>
          )}
        </div>
      </div>
    </div>
  );
};

export default LegalConsentModalViewOnly;