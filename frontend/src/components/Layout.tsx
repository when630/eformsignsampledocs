import React from 'react';
import Header from './Header';
import SideBar from './SideBar';
import './Layout.css';

const Layout = ({ children, onCategoryClick }: { children: React.ReactNode, onCategoryClick: (id: number) => void }) => {
  return (
    <div className="layout-container">
      <SideBar onCategoryClick={onCategoryClick} />
      <div className="content-column">
        <Header />
        <div className="content-area">{children}</div>
      </div>
    </div>
  );
};

export default Layout;