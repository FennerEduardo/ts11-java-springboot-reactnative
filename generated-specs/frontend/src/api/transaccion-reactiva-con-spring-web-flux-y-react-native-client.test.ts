import { ApiError, COMMANDS, createTransaccionReactivaConSpringWebFluxYReactNativeClient } from './transaccion-reactiva-con-spring-web-flux-y-react-native-client';

function fakeFetch(status: number, body: unknown) {
  return jest.fn(async (_url: RequestInfo | URL, _init?: RequestInit) => new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } }));
}

describe('TransaccionReactivaConSpringWebFluxYReactNative API client', () => {
  it('exposes one method per domain command', () => {
    expect(COMMANDS).toEqual(['process_transaccion_reactiva_con_spring_web_flux_y_react_native']);
  });

  it('posts the command with tenant and idempotency headers', async () => {
    const fetch = fakeFetch(201, { type: 'EmiteFlujoServerSentEventFluxConsumidoPorUseQueryEnReactNati', aggregateId: 'agg-1', version: 1 });
    const client = createTransaccionReactivaConSpringWebFluxYReactNativeClient({ baseUrl: 'https://api.test/', tenantId: 'acme', fetch });

    const result = await client.processTransaccionReactivaConSpringWebFluxYReactNative('agg-1', { amount: 100 }, { idempotencyKey: 'key-1' });

    expect(result).toEqual({ type: 'EmiteFlujoServerSentEventFluxConsumidoPorUseQueryEnReactNati', aggregateId: 'agg-1', version: 1 });
    const [url, init] = fetch.mock.calls[0];
    expect(url).toBe('https://api.test/api/v1/transaccion-reactiva-con-spring-web-flux-y-react-native/agg-1/process_transaccion_reactiva_con_spring_web_flux_y_react_native');
    expect(init?.method).toBe('POST');
    expect((init?.headers as Record<string, string>)['X-Tenant-Id']).toBe('acme');
    expect((init?.headers as Record<string, string>)['X-Idempotency-Key']).toBe('key-1');
    expect(JSON.parse(String(init?.body))).toEqual({ amount: 100 });
  });

  it('raises ApiError with the server detail on failure', async () => {
    const client = createTransaccionReactivaConSpringWebFluxYReactNativeClient({ fetch: fakeFetch(422, { detail: 'Command id is required' }) });
    await expect(client.execute('agg-1', 'process_transaccion_reactiva_con_spring_web_flux_y_react_native')).rejects.toEqual(new ApiError(422, 'Command id is required'));
  });
});
