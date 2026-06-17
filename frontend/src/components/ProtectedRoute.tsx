import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';

interface ProtectedRouteProps {
    children: React.ReactNode;
    requiredRole?: 'PARENT' | 'CHILD';
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredRole }) => {
    const { user, isAuthenticated } = useAuth();
    const location = useLocation();

    // While checking auth status on mount, you might want to show a loading spinner
    // but since we read from localStorage synchronously in AuthProvider, 
    // we can rely on isAuthenticated immediately.

    if (!isAuthenticated) {
        // Redirect to appropriate login page based on where they tried to go
        const isChildRoute = location.pathname.startsWith('/child-portal');
        return <Navigate to={isChildRoute ? "/child-login" : "/login"} state={{ from: location }} replace />;
    }

    if (requiredRole && user?.role !== requiredRole) {
        // User has wrong role, redirect to their default home
        return <Navigate to={user?.role === 'CHILD' ? "/child-portal" : "/dashboard"} replace />;
    }

    return <>{children}</>;
};

export default ProtectedRoute;
