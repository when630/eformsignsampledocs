import { Routes, Route } from 'react-router-dom';
import LoginPage from './pages/login';
import RegisterPage from './pages/register';
import MainPage from './pages';
import ProtectedRoute from './components/ProtectedRoute';
import SearchResultsPage from './pages/search';


function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <MainPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/search"
        element={
          <ProtectedRoute>
            <SearchResultsPage />
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}

export default App;