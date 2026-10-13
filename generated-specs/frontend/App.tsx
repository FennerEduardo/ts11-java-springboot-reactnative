import { ScrollView } from 'react-native';
import { TransaccionReactivaConSpringWebFluxYReactNativeScreen } from './src/TransaccionReactivaConSpringWebFluxYReactNativeScreen';

const baseUrl = process.env.EXPO_PUBLIC_API_URL;

export default function App() {
  return (
    <ScrollView>
      <TransaccionReactivaConSpringWebFluxYReactNativeScreen baseUrl={baseUrl} />
    </ScrollView>
  );
}
