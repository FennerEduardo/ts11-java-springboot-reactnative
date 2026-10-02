import { fireEvent, render, screen } from '@testing-library/react-native';
import { TransaccionReactivaConSpringWebFluxYReactNativeScreen } from './TransaccionReactivaConSpringWebFluxYReactNativeScreen';
import type { TransaccionReactivaConSpringWebFluxYReactNativeClient } from './api/transaccion-reactiva-con-spring-web-flux-y-react-native-client';

describe('TransaccionReactivaConSpringWebFluxYReactNativeScreen', () => {
  it('executes a command and lists the resulting event', async () => {
    const execute = jest.fn().mockResolvedValue({ type: 'EmiteFlujoServerSentEventFluxConsumidoPorUseQueryEnReactNati', aggregateId: 'agg-1', version: 1 });
    render(<TransaccionReactivaConSpringWebFluxYReactNativeScreen client={{ execute } as unknown as TransaccionReactivaConSpringWebFluxYReactNativeClient} />);

    fireEvent.changeText(screen.getByTestId('aggregate-id'), 'agg-1');
    fireEvent.press(screen.getByTestId('process_transaccion_reactiva_con_spring_web_flux_y_react_native'));

    expect(await screen.findByText('EmiteFlujoServerSentEventFluxConsumidoPorUseQueryEnReactNati v1')).toBeTruthy();
    expect(execute).toHaveBeenCalledWith('agg-1', 'process_transaccion_reactiva_con_spring_web_flux_y_react_native', undefined, expect.objectContaining({ idempotencyKey: expect.any(String) }));
  });

  it('shows the backend error', async () => {
    const execute = jest.fn().mockRejectedValue(new Error('Command id is required'));
    render(<TransaccionReactivaConSpringWebFluxYReactNativeScreen client={{ execute } as unknown as TransaccionReactivaConSpringWebFluxYReactNativeClient} />);

    fireEvent.changeText(screen.getByTestId('aggregate-id'), 'agg-1');
    fireEvent.press(screen.getByTestId('process_transaccion_reactiva_con_spring_web_flux_y_react_native'));

    expect(await screen.findByText('Command id is required')).toBeTruthy();
  });
});
