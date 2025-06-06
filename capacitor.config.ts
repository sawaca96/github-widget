import type { CapacitorConfig } from '@capacitor/cli';
import ip from 'ip';


let config: CapacitorConfig;

const baseConfig: CapacitorConfig = {
  appId: 'com.sawaca96.githubwidget',
  appName: 'GithubWidget',
  webDir: 'dist',
  plugins: {
    FirebaseAuthentication: {
      skipNativeAuth: false,
      providers: ['github.com']
    }
  }
};

switch (process.env.NODE_ENV) {
  case 'production':
    config = {
      ...baseConfig,
    };
    break;
  default:
    config = {
      ...baseConfig,
      server: {
        url: `http://${ip.address()}:5173/`,
        cleartext: true,
      }
    };
    break;
}

export default config;
