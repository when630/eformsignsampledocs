export type Document = {
  id: number;
  title: string;
  storageId: number;
};

export type Category = {
  id: number;
  name: string;
  children: Category[];
};

export type CopyrightView = {
  copyrightId: number | null;
  type: 'PUBLIC_LICENSE' | 'GENERAL' | null;
  name: string | null;          // 출처 기관
  uploaderName: string | null;
  url: string | null;           // 기관 링크 (있을 때만 a태그)
  published: string | null;     // "yyyy-MM-dd" 또는 null
  displayText: string | null;   // 백엔드가 만든 문구 (지금은 "이폼사인")
};