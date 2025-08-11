import React, { useEffect, useMemo } from 'react';
import './LegalConsentModal.css'

type Props = {
  open: boolean;
  onClose: () => void;
  storageKey?: string;        // 기본: 'signup.legalConsent' 
};

const LegalConsentModalViewOnly: React.FC<Props> = ({
  open,
  onClose,
  storageKey = 'signup.legalConsent',
}) => {
  const [agreed, setAgreed] = React.useState(false);

  useEffect(() => {
    if (!open) return;
    const saved = localStorage.getItem(storageKey);
    setAgreed(saved === 'true');
  }, [open, storageKey]);

  // 스크롤 잠금
  useEffect(() => {
    if (!open) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => { document.body.style.overflow = prev; };
  }, [open]);

  const statusLabel = useMemo(() => (agreed ? '동의 완료' : '동의하지 않음'), [agreed]);

  if (!open) return null;

  return (
    <div className="legal-modal-overlay" role="dialog" onClick={onClose} aria-modal="true" aria-labelledby="legal-modal-title">
      <div className="legal-modal">
        <div className="legal-modal-header">
          <div className="legal-modal-header-left">
            <h3 id="legal-modal-title" className="legal-modal-title">서식 안내</h3>
            {statusLabel && <span className="status-badge ok">{statusLabel}</span>}
          </div>
          <button className="legal-modal-close" onClick={onClose} aria-label="닫기">×</button>
        </div>

        <div className="legal-modal-body">

          <section className="legal-card" aria-labelledby="legal-title">
            <h3 id="legal-title" className="legal-title">서식에 관한 안내</h3>
            <p className="legal-text">
            서식의 내용은 사용자가 수정, 보완, 삭제할 수 있으며, 이러한 변경이 이루어진 경우를 포함하여 해당 서식의 내용에 대하여 서식 제공자 및 이폼사인은 법적 효력을 보증하지 않습니다.<br/>
            서식을 그대로 사용하거나, 수정·보완·삭제하여 사용함으로써 발생하는 분쟁 또는 손해(제3자와의 분쟁 및 손해를 포함)에 대하여 서식 제공자와 이폼사인은 법적 책임을 지지 않습니다.<br/>
            사용자는 서식을 이폼사인 에디터 내에서만 사용할 수 있으며, 서식의 무단 유출, 배포 등 외부 사용은 엄격히 금지됩니다.
          </p>

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