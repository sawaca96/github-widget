import { createRouter, createWebHistory } from '@ionic/vue-router';
import { RouteRecordRaw } from 'vue-router';
import HomePage from '../views/HomePage.vue'
import LoginPage from '../views/LoginPage.vue'
import { FirebaseAuthentication } from '@capacitor-firebase/authentication'
import { currentUser } from '../services/auth'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'Home',
    component: HomePage,
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginPage
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

const checkAuthState = async (): Promise<boolean> => {
  try {
    if (currentUser.value) {
      return true;
    }

    const result = await FirebaseAuthentication.getCurrentUser();
    return !!result.user;
  } catch (error) {
    console.error('인증 상태 확인 오류:', error);
    return false;
  }
};

// 네비게이션 가드 설정
router.beforeEach(async (to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  
  try {
    const isAuthenticated = await checkAuthState();
    
    if (requiresAuth && !isAuthenticated) {
      // 인증이 필요한 페이지에 접근하려고 하는데 로그인되어 있지 않은 경우
      next('/login');
    } else if (to.path === '/login' && isAuthenticated) {
      // 이미 로그인된 상태에서 로그인 페이지에 접근하려는 경우
      next('/home');
    } else {
      // 그 외의 경우
      next();
    }
  } catch (error) {
    console.error('라우터 인증 상태 확인 오류:', error);
    if (requiresAuth) {
      next('/login');
    } else {
      next();
    }
  }
});

export default router
