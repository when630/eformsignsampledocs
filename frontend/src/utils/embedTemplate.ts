// src/utils/embedTemplate.ts

type EformsignTemplateOption = {
  account: {
    id: number;
    name: string;
    email: string;
    companyId: string;
  }
  token: {
    access_token: string;
    refresh_token: string;
  };
  document: {
    id: number;
    title: string;
    storageId: number;
  }
  base64: string;
};

export function openEformsignTemplate(option: EformsignTemplateOption) {
  const newWindow = window.open('/embed.html', '_blank');

  const waitAndPost = () => {
    if (newWindow && newWindow.postMessage) {
      newWindow.postMessage(option, '*');
    } else {
      setTimeout(waitAndPost, 100);
    }
  };

  setTimeout(waitAndPost, 300); // 약간의 딜레이 후 메시지 전송
}