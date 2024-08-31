import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { MatCardModule, MatInputModule, MatPaginatorModule } from '@angular/material';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from '../../app-routing.module';
import { UserTransfersComponent } from '../user-transfers/user-transfers.component';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { UserToken } from '../../models/user-token';
import { MatTableModule } from '@angular/material/table';
import { APP_BASE_HREF } from '@angular/common';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
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
        LoginComponent,
        UserTransfersComponent
      ],
      providers: [
        {provide: APP_BASE_HREF, useValue: '/'}
      ]
    })
    .compileComponents();
  }));

  const USERNAME = 'username';
  const PASSWORD = 'password';

  const authService: any = {
    authenticateUser: jasmine.createSpy('authenticateUser')
      .and.returnValue(of(new UserToken())),
    saveToken: jasmine.createSpy('saveToken').and.stub()
  };

  const router: any = {
    navigate: jasmine.createSpy('navigate').and.stub()
  };

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    component.authService = authService;
    component.router = router;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('on submit should authenticate user', () => {
    // Arrange
    const controls = {
      username: {
        value: USERNAME
      },
      password: {
        value: PASSWORD
      }
    };
    const form: any = {};
    form.controls = controls;
    form.valid = true;
    component.form = form;

    // Act
    component.submit();

    // Assert
    expect(authService.authenticateUser).toHaveBeenCalledWith(USERNAME, PASSWORD);
    expect(router.navigate).toHaveBeenCalledWith(['/transfers']);
    expect(authService.saveToken).toHaveBeenCalled();
  });

});
