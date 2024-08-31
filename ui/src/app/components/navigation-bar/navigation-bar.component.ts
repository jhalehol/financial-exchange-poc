import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-navigation-bar',
  templateUrl: './navigation-bar.component.html',
  styleUrls: ['./navigation-bar.component.css']
})
export class NavigationBarComponent implements OnInit {

  userName: String;
  isAuthenticated$: Observable<boolean>;

  constructor(public authService: AuthService) { }

  ngOnInit() {
    this.isAuthenticated$ = this.authService.isAuthenticated();
    this.isAuthenticated$.subscribe(isLoggedIn => {
      if (isLoggedIn) {
        this.userName = this.authService.getUserName();
      }
    });
  }

  logoutUser() {
    this.authService.removeSessionData();
  }

}
