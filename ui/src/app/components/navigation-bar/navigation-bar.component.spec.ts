import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NavigationBarComponent } from './navigation-bar.component';
import { MatToolbarModule } from '@angular/material/toolbar';
import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from '../../app-routing.module';
import { LoginComponent } from '../login/login.component';
import { UserTransfersComponent } from '../user-transfers/user-transfers.component';
import { MatCardModule, MatInputModule, MatPaginatorModule } from '@angular/material';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatTableModule } from '@angular/material/table';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { APP_BASE_HREF } from '@angular/common';
import { of } from 'rxjs';

describe('NavigationBarComponent', () => {
  let component: NavigationBarComponent;
  let fixture: ComponentFixture<NavigationBarComponent>;

  const authService: any = {
    isAuthenticated: jasmine.createSpy('isAuthenticated')
      .and.returnValue(of(true)),
    removeSessionData: jasmine.createSpy('removeSessionData').and.stub(),
    getUserName: jasmine.createSpy('getUserName').and.returnValue('Pedro')
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        MatToolbarModule,
        MatCardModule,
        FormsModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        HttpClientModule,
        AppRoutingModule,
        MatSelectModule,
        MatDatepickerModule,
        MatTableModule,
        MatPaginatorModule,
        MatFormFieldModule,
        MatInputModule,
        BrowserAnimationsModule
      ],
      declarations: [
        NavigationBarComponent,
        LoginComponent,
        UserTransfersComponent
      ],
      providers: [
        {provide: APP_BASE_HREF, useValue: '/'}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NavigationBarComponent);
    component = fixture.componentInstance;
    component.authService = authService;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('on init', () => {
    // Act
    component.ngOnInit();

    // Assert
    expect(authService.isAuthenticated).toHaveBeenCalled();
  });

  it('on logout', () => {
    // Act
    component.logoutUser();

    // Assert
    expect(authService.removeSessionData).toHaveBeenCalled();
  });
});
