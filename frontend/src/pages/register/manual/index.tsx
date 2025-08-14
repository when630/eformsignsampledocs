import React, { useEffect, useMemo, useState } from 'react';
import './style.css';

/** 각 단계(step) 타입 */
type Step = {
  img: string;        // 이미지 경로(예: /assets/manual/api/01.png)
  alt?: string;
  title?: string;
  desc?: React.ReactNode;
};

/** 공통 Step 뷰어 */
function StepViewer({ steps }: { steps: Step[] }) {
  const [idx, setIdx] = useState(0);
  const total = steps.length;
  const cur = steps[idx];

  const go = (n: number) => setIdx((i) => Math.min(Math.max(i + n, 0), total - 1));
  const set = (i: number) => setIdx(i);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'ArrowLeft') go(-1);
      if (e.key === 'ArrowRight') go(1);
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [total]);

  return (
  <section className="manual-card" aria-label="manual step viewer">

    {/* 버튼을 이미지 위로 이동 */}
    <div className="manual-controls top-controls">
      <button
        className="btn"
        onClick={() => go(-1)}
        disabled={idx === 0}
        aria-label="이전 단계"
      >
        ← 이전
      </button>
      <div className="manual-progress">
        {idx + 1} / {total}
      </div>
      <button
        className="btn btn-primary"
        onClick={() => go(1)}
        disabled={idx === total - 1}
        aria-label="다음 단계"
      >
        다음 →
      </button>
    </div>

    <div className="manual-stage">
      <img
        src={cur.img}
        alt={cur.alt || cur.title || `step-${idx + 1}`}
        className="manual-image"
      />
    </div>

    <div className="manual-description">
      {cur.title && <h3 className="manual-step-title">{cur.title}</h3>}
      {cur.desc && <div className="manual-step-desc">{cur.desc}</div>}
    </div>

    {total > 1 && (
      <div className="manual-thumbs" role="listbox" aria-label="단계 썸네일">
        {steps.map((s, i) => (
          <button
            key={i}
            className={`thumb ${i === idx ? 'active' : ''}`}
            onClick={() => set(i)}
            aria-selected={i === idx}
            title={s.title || `step-${i + 1}`}
          >
            <img src={s.img} alt={s.alt || s.title || `thumb-${i + 1}`} />
          </button>
        ))}
      </div>
    )}
  </section>
);
}

/** 탭 컴포넌트 */
function Tabs<T extends string>({
  tabs,
  value,
  onChange,
}: {
  tabs: { key: T; label: string }[];
  value: T;
  onChange: (v: T) => void;
}) {
  return (
    <div className="tabs">
      {tabs.map((t) => (
        <button
          key={t.key}
          className={`tab ${value === t.key ? 'active' : ''}`}
          onClick={() => onChange(t.key)}
          role="tab"
          aria-selected={value === t.key}
        >
          {t.label}
        </button>
      ))}
    </div>
  );
}

/** 실제 페이지 */
const ManualPage: React.FC = () => {
  type TabKey = 'API' | 'COMPANY';

  // URL ?tab=COMPANY 로 초기 탭 선택 지원
  const initialTab = (() => {
    const p = new URLSearchParams(window.location.search).get('tab');
    return (p === 'COMPANY' || p === 'API') ? (p as TabKey) : 'API';
  })();

  const [tab, setTab] = useState<TabKey>(initialTab);

  const apiSteps: Step[] = useMemo(
    () => [
      {
        img: 'manual/mainpage.png',
        title: '이폼사인 메인페이지 접속 및 메뉴 오픈',
        desc: <>eformsign 메인페이지에서 메뉴를 열어주세요.</>
      },
      {
        img: 'manual/menu_open.png',
        title: '사이드 메뉴 열기',
        desc: <>사이드 메뉴를 스크롤하여 아래로 내려주세요.</>
      },
      {
        img: 'manual/menu_open_scrolldown_api.png',
        title: '커넥트 메뉴 열기',
        desc: <> <strong>커넥트</strong> 메뉴를 눌러 케넥트 메뉴를 열어주세요.</>
      },
      {
        img: 'manual/menu_open_connect.png',
        title: 'API / Webhook 메뉴 클릭',
        desc: <> <strong>API / Webhook</strong> 메뉴를 눌러 API 페이지를 열어주세요.</>
      },
      {
        img: 'manual/api_webhook_page.png',
        title: 'API 키 생성',
        desc: <> <strong>API 키 생성</strong>을 눌러 API 키를 생성합니다.</>
      },
      {
        img: 'manual/making_key.png',
        title: 'API 키 생성 상세 내용',
        desc: <>별칭, 애플리케이션 이름, 값(secret key)를 입력해주시고 검증 유형을 반드시 <strong>"Bearer token"</strong>으로 해주세요. 그리고 <strong>저장</strong>을 눌러 키를 생성해주세요.</>
      },
      {
        img: 'manual/after_make_key.png',
        title: 'API 키 생성 후',
        desc: <>키를 생성 하신 후 <strong>키 보기</strong>를 눌러 API 키와 secret 키를 확인해주세요.</>
      },
      {
        img: 'manual/api_view_key.png',
        title: 'API 키 및 secret 키 확인',
        desc: <> <strong>복사</strong> 버튼을 눌러 <strong>API 키</strong>와 <strong>비밀 키</strong>를 회원가입 페이지에 기입해주세요.</>
      },
    ],
    []
  );

  const companySteps: Step[] = useMemo(
    () => [
      { img: 'manual/mainpage.png', title: '이폼사인 메인페이지 접속 및 메뉴 오픈', desc: <>eformsign 메인페이지에서 메뉴를 열어주세요.</> },
      { img: 'manual/menu_open.png', title: '사이드 메뉴 열기', desc: <>사이드 메뉴를 스크롤하여 아래로 내려주세요.</> },
      { img: 'manual/menu_open_scrolldown_company.png', title: '회사 관리 메뉴 열기', desc: <> <strong>회사 관리</strong>메뉴를 눌러 회사 관리 메뉴를 열어주세요.</> },
      { img: 'manual/menu_open_company.png', title: '회사 정보 메뉴 클릭', desc: <> <strong>회사 정보</strong>메뉴를 눌러 회사 정보 페이지를 열어주세요.</> },
      { img: 'manual/company_info.png', title: '회사 ID 확인', desc: <> <strong>회사 ID</strong>를 드래그 하여 복사 후 회원가입 페이지에 기입해주세요.</> },
    ],
    []
  );

  const tabs = useMemo(
    () => [
      { key: 'API' as const, label: 'API 키 발급 / 확인' },
      { key: 'COMPANY' as const, label: '회사 ID 확인' },
    ],
    []
  );

  return (
    <div className="manual-wrapper">
      <div className="manual-topbar">
        <h2 className="manual-title">회원가입 매뉴얼</h2>
      </div>

      <div className="manual-help">
        <p>
          ※ 복사한 <strong>API Key</strong>, <strong>Secret Key</strong>, <strong>Company ID</strong>는
          <strong> 회원가입</strong> 페이지의 입력란에 그대로 붙여넣어 주세요.
        </p>
      </div>

      <Tabs<TabKey> tabs={tabs} value={tab} onChange={setTab} />

      {tab === 'API' && <StepViewer steps={apiSteps} />}
      {tab === 'COMPANY' && <StepViewer steps={companySteps} />}
    </div>
  );
};

export default ManualPage;