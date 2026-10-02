import { useMemo, useState } from 'react';
import { Button, Text, TextInput, View } from 'react-native';
import { COMMANDS, CommandName, CommandResult, createTransaccionReactivaConSpringWebFluxYReactNativeClient, TransaccionReactivaConSpringWebFluxYReactNativeClient } from './api/transaccion-reactiva-con-spring-web-flux-y-react-native-client';

const newKey = () => globalThis.crypto?.randomUUID?.() ?? Math.random().toString(36).slice(2);

export function TransaccionReactivaConSpringWebFluxYReactNativeScreen({ client, baseUrl }: { client?: TransaccionReactivaConSpringWebFluxYReactNativeClient; baseUrl?: string }) {
  const api = useMemo(() => client ?? createTransaccionReactivaConSpringWebFluxYReactNativeClient({ baseUrl }), [client, baseUrl]);
  const [aggregateId, setAggregateId] = useState('');
  const [events, setEvents] = useState<CommandResult[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function run(command: CommandName) {
    setLoading(true);
    setError(null);
    try {
      const event = await api.execute(aggregateId, command, undefined, { idempotencyKey: newKey() });
      setEvents(prev => [...prev, event]);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Request failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <View accessibilityLabel="Transacción Reactiva con Spring WebFlux y React Native">
      <Text accessibilityRole="header">Transacción Reactiva con Spring WebFlux y React Native</Text>
      <TextInput testID="aggregate-id" placeholder="Aggregate id" value={aggregateId} onChangeText={setAggregateId} />
      {COMMANDS.map(command => (
        <Button key={command} testID={command} title={command} disabled={!aggregateId || loading} onPress={() => run(command)} />
      ))}
      {error ? <Text accessibilityRole="alert">{error}</Text> : null}
      {events.map(e => (
        <Text key={`${e.aggregateId}-${e.version}`}>
          {e.type} v{e.version}
        </Text>
      ))}
    </View>
  );
}
