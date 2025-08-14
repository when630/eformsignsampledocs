// src/pages/index.tsx (또는 MainPage 경로)
import { useEffect, useMemo, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import { getDocumentsByCategory, getCategoryPath, getCategoryTree } from '../services/api';
import './style.css';
import type { Document, Category } from '../utils/types';
import DocumentModal from '../components/DocumentModal';

const GAP_PX = 24; // .document-grid gap 과 동일해야 함

const MainPage = () => {
  const [tree, setTree] = useState<Category[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [depth2List, setDepth2List] = useState<Category[]>([]);
  const [documents, setDocuments] = useState<Document[]>([]);

  const [selectedDocument, setSelectedDocument] = useState<Document | null>(null);
  const [loadedMap, setLoadedMap] = useState<Record<number, boolean>>({});
  const [categoryPath, setCategoryPath] = useState<string>('');

  const location = useLocation() as { state?: { categoryId?: number } };
  const navigate = useNavigate();
  const appliedFromRouteRef = useRef(false);

  // === 마지막 줄 왼쪽 정렬용 측정 레퍼런스 & 폭 상태 ===
  const gridWrapRef = useRef<HTMLDivElement>(null);
  const gridRef = useRef<HTMLDivElement>(null);
  const [gridInnerWidth, setGridInnerWidth] = useState<number | 'auto'>('auto');

  const depth2Descriptions: Record<string, string> = {
    '인사·채용·노무팀': '인력 채용, 근로계약, 복리후생, 휴가·휴직 관리, 퇴직 및 인사 이동 관련 문서를 처리하며, 노사관계와 인사 기록 전반을 관리합니다.',
    '재무·회계·세무팀': '예산 수립·집행, 지출 결의, 세금 신고, 4대보험 처리, 재무 보고 등 회사 자금 및 회계 관련 문서를 다룹니다.',
    '법무·리스크관리팀': '각종 계약서(매매, 용역, MOU 등), 법률 자문, 분쟁 대응, 컴플라이언스 점검, 개인정보 보호 등 법적·규제 리스크 관련 문서를 관리합니다.',
    '총무·시설·행정팀': '비품·물품 관리, 인감 등록, 시설 유지보수, 임대차 계약, 공문 처리 등 조직 운영 지원과 시설·행정 관련 문서를 담당합니다.',
    '영업·마케팅·제휴팀': '제안서, 제휴의향서, 입점·공급 계약, 마케팅·캠페인 계획, 실적 보고 등 영업 추진 및 파트너십·홍보 관련 문서를 작성·관리합니다.',
    '기획·프로젝트팀': '사업·프로젝트 기획서, 제안서, 손익 분석, 정책·지원 신청, 성과 보고 등 사업 실행 및 프로젝트 운영에 필요한 문서를 다룹니다.',
    '안전·환경·시설관리팀': '안전 점검표, 사고 보고서, 산업안전보건 관련 계획·내역, 폐기물 관리, 재해 보고 등 사업장 안전·환경 규제 준수 관련 문서를 관리합니다.',
    '기술·R&D·콘텐츠팀': '기술 이전·라이선스 계약, 콘텐츠 제작·저작권 계약, SW 개발·유지관리 표준계약서, 버전 배포 계획 등 기술·콘텐츠 개발 관련 문서를 다룹니다.',
    '경영·전략·경영지원팀': '연간 경영계획, 투자·업무 협약, 이사회 회의록, 회원·고객 동의서 등 조직 전략 수립과 전사 경영 지원에 필요한 문서를 관리합니다.',
  };

  const depth2Icons: Record<string, string> = {
    '인사·채용·노무팀': '/icons/group.png',
    '재무·회계·세무팀': '/icons/dollar-circle.png',
    '법무·리스크관리팀': '/icons/gavel.png',
    '총무·시설·행정팀': '/icons/buildings.png',
    '영업·마케팅·제휴팀': '/icons/megaphone-alt.png',
    '기획·프로젝트팀': '/icons/clipboard-check.png',
    '안전·환경·시설관리팀': '/icons/hard-hat.png',
    '기술·R&D·콘텐츠팀': '/icons/light-bulb-alt.png',
    '경영·전략·경영지원팀': '/icons/chart-spline.png',
  };

  // 트리 초기 로드
  useEffect(() => {
    let alive = true;
    getCategoryTree()
      .then((t) => { if (alive) setTree(t || []); })
      .catch((e) => console.error('카테고리 트리 로드 실패', e));
    return () => { alive = false; };
  }, []);

  // 라우팅/세션 categoryId 1회 적용
  useEffect(() => {
    if (appliedFromRouteRef.current) return;
    const stateId = location.state?.categoryId;
    const storedId = sessionStorage.getItem('pendingCategoryId');

    if (stateId != null) {
      setSelectedId(Number(stateId));
      appliedFromRouteRef.current = true;
      navigate('.', { replace: true, state: {} });
      return;
    }
    if (storedId) {
      setSelectedId(Number(storedId));
      appliedFromRouteRef.current = true;
      sessionStorage.removeItem('pendingCategoryId');
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // id → 노드/부모 탐색
  const findNode = useMemo(() => {
    const byId: Record<number, { node: Category; parentId: number | null }> = {};
    tree.forEach((d1) => {
      byId[d1.id] = { node: d1, parentId: null };
      d1.children?.forEach((d2) => {
        byId[d2.id] = { node: d2, parentId: d1.id };
      });
    });
    return (id: number) => byId[id] || null;
  }, [tree]);

  // 선택 변경 시 뷰 갱신
  useEffect(() => {
    if (!selectedId) return;
    const found = findNode(selectedId);
    if (!found) {
      setDepth2List([]);
      setDocuments([]);
      setCategoryPath('');
      return;
    }

    const { node, parentId } = found;

    // Depth1 선택 → Depth2 목록 표시
    if (parentId === null) {
      setDocuments([]);
      setLoadedMap({});
      setDepth2List(node.children || []);
      const d1name = node.name;
      const count = (node.children || []).length;
      setCategoryPath(`${d1name} (분류 ${count}개)`);
      return;
    }

    // Depth2 선택 → 문서 로드
    let alive = true;
    Promise.all([getDocumentsByCategory(node.id), getCategoryPath(node.id)])
      .then(([docs, path]) => {
        if (!alive) return;
        setDepth2List([]);

        const m: Record<number, boolean> = {};
        (docs as Document[]).forEach((d: Document) => { m[d.id] = false; });
        setLoadedMap(m);

        setDocuments(docs);
        setCategoryPath(`${(path || []).join(' > ')} (총 ${docs.length}개 문서)`);
      })
      .catch((err) => console.error('문서 또는 카테고리 경로 로드 실패', err));

    return () => { alive = false; };
  }, [selectedId, findNode]);

  const handleImageLoad = (id: number) => {
    setLoadedMap((prev) => ({ ...prev, [id]: true }));
  };

  const handleCategoryClick = (id: number) => setSelectedId(id);
  const openDepth2 = (id: number) => setSelectedId(id);

  const isShowingDepth2List = depth2List.length > 0;

  // === 마지막 줄 왼쪽 정렬을 위한 그리드 폭 계산 ===
  const recalcGridWidth = () => {
    const wrap = gridWrapRef.current;
    const grid = gridRef.current;
    if (!wrap || !grid) { setGridInnerWidth('auto'); return; }

    // 첫 카드의 실제 폭 측정 (패딩/보더 포함)
    const firstCard = grid.querySelector<HTMLElement>('.document-card');
    if (!firstCard) { setGridInnerWidth('auto'); return; }

    const wrapWidth = wrap.clientWidth;
    const cardWidth = firstCard.offsetWidth;

    // 한 줄에 들어갈 카드 개수 (최소 1)
    const perRow = Math.max(1, Math.floor((wrapWidth + GAP_PX) / (cardWidth + GAP_PX)));

    // 내부 그리드 실제 폭 = 카드*개수 + 갭*(개수-1)
    const inner =
      perRow * cardWidth + (perRow - 1) * GAP_PX;

    setGridInnerWidth(inner);
  };

  useEffect(() => {
    // 문서 목록 바뀌거나, 창 크기 바뀔 때 다시 계산
    recalcGridWidth();
    const onResize = () => recalcGridWidth();
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [documents.length]);

  return (
    <div className="main-container">
      <Layout onCategoryClick={handleCategoryClick}>
        <div className="category-info">
          {categoryPath && <p>{categoryPath}</p>}
          {!selectedId && <p>안녕하세요 이폼샘플입니다. 원하시는 카테고리를 좌측 메뉴에서 골라보세요.</p>}
        </div>

        {/* Depth1: 소분류(Depth2) 목록 */}
        {isShowingDepth2List && (
          <div className="category-grid">
            {depth2List.map((c) => (
              <div
                key={c.id}
                className="category-card"
                onClick={() => openDepth2(c.id)}
                role="button"
                title={c.name}
              >
                <img
                  src={depth2Icons[c.name]}
                  alt={`${c.name} 아이콘`}
                  className="category-icon"
                  loading="lazy"
                />
                <div className="category-content">
                  <div className="category-title">{c.name}</div>
                  <div className="category-description">
                    {depth2Descriptions[c.name] || ''}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Depth2: 문서(Depth3) 목록 */}
        {!isShowingDepth2List && selectedId && (
          // 바깥 래퍼: 가운데 정렬 역할만 담당
          <div ref={gridWrapRef} style={{ padding: '2rem' }}>
            <div
              ref={gridRef}
              className="document-grid"
              style={{
                width: gridInnerWidth === 'auto' ? 'auto' : `${gridInnerWidth}px`,
                margin: '0 auto',                // 전체는 가운데 정렬
                justifyContent: 'flex-start',    // 내부 줄은 왼쪽부터
              }}
            >
              {documents.map((doc) => (
                <div
                  key={doc.id}
                  className="document-card"
                  onClick={() => setSelectedDocument(doc)}
                >
                  {!loadedMap[doc.id] && (
                    <div className="thumbnail-loading">썸네일 로딩 중...</div>
                  )}
                  <img
                    src={`http://localhost:8080/api/documents/thumbnail/${doc.storageId}`}
                    alt="썸네일"
                    className="doc-thumbnail"
                    style={{ display: loadedMap[doc.id] ? 'block' : 'none' }}
                    onLoad={() => handleImageLoad(doc.id)}
                    onError={() => handleImageLoad(doc.id)}
                  />
                  <div className="doc-title">{doc.title}</div>
                </div>
              ))}
              {documents.length === 0 && (
                <div className="search-empty">등록된 문서가 없습니다.</div>
              )}
            </div>
          </div>
        )}
      </Layout>

      {selectedDocument && (
        <DocumentModal
          document={selectedDocument}
          onClose={() => setSelectedDocument(null)}
        />
      )}
    </div>
  );
};

export default MainPage;