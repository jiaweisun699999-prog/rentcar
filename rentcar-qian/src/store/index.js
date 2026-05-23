import { defineStore, createPinia } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    token: localStorage.getItem('token') || ''
  }),
  actions: {
    setToken(token) {
      this.token = token;
      localStorage.setItem('token', token);
    },
    setUserInfo(info) {
      this.userInfo = info;
    },
    logout() {
      this.token = '';
      this.userInfo = null;
      localStorage.removeItem('token');
    }
  }
});

const pinia = createPinia();
export default pinia;
