import { defineStore, createPinia } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null'),
    token: localStorage.getItem('token') || ''
  }),
  getters: {
    isAdmin: (state) => state.userInfo?.role === 'admin' || state.userInfo?.role === 'store_admin',
    isSuperAdmin: (state) => state.userInfo?.role === 'admin',
    isLoggedIn: (state) => !!state.token
  },
  actions: {
    setToken(token) {
      this.token = token;
      localStorage.setItem('token', token);
    },
    setUserInfo(info) {
      this.userInfo = info;
      localStorage.setItem('userInfo', JSON.stringify(info));
    },
    logout() {
      this.token = '';
      this.userInfo = null;
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
    }
  }
});

const pinia = createPinia();
export default pinia;
