import { createRouter, createWebHistory } from "@ionic/vue-router";
import { RouteRecordRaw } from "vue-router";
import HomePage from "../views/HomePage.vue";
import LoginPage from "../views/LoginPage.vue";
import { logOut } from "../services/auth";
import { getGitHubToken } from "@/services/github";
import { FirebaseAuthentication } from "@capacitor-firebase/authentication";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    redirect: "/home",
  },
  {
    path: "/home",
    name: "Home",
    component: HomePage,
    meta: { requiresAuth: true },
  },
  {
    path: "/login",
    name: "Login",
    component: LoginPage,
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

const checkAuthState = async (): Promise<boolean> => {
  try {
    const currentUser = await FirebaseAuthentication.getCurrentUser();
    const result = await getGitHubToken();
    if (result.token && !currentUser.user) {
      await logOut();
    }
    return !!result.token && !!currentUser.user;
  } catch (error) {
    return false;
  }
};

router.beforeEach(async (to, from, next) => {
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth);

  try {
    const isAuthenticated = await checkAuthState();

    if (requiresAuth && !isAuthenticated) {
      next("/login");
    } else if (to.path === "/login" && isAuthenticated) {
      next("/home");
    } else {
      next();
    }
  } catch (error) {
    next("/login");
  }
});

export default router;
