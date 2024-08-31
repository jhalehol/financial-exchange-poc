import { TestBed } from '@angular/core/testing';

import { AuthGuard } from './auth.guard';
import { of } from 'rxjs';

describe('AuthGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AuthGuard]
    });
  });

  it('can Activate should return true when is loggedIn', () => {
    // Arrange
    const authServiceSpy: any = {
      isAuthenticated: jasmine.createSpy('isAuthenticated').and.returnValue(of(true))
    };

    const service = new AuthGuard(authServiceSpy, null);

    // Act & Assert
    service.canActivate(null, null)
      .subscribe(result => {
        expect(result).toBe(true);
      });
  });
});
