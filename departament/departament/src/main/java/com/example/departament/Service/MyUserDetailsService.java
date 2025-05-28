package com.example.departament.Service;

import com.example.departament.Entity.User;
import com.example.departament.Repository.RolRepository;
import com.example.departament.Repository.UserRepository;
import com.example.departament.config.JwtUtil;
import com.example.departament.config.MyUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository usuariosR;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = usuariosR.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (u == null) {
            throw new UsernameNotFoundException(username);
        }

        u.setPassword("{noop}" + u.getPassword());
        //se extrae los nombres de los roles
        List<String> roles = u.getRoles().stream()
                .map(rol ->"ROLE_" + rol.getNombre().name())
                .collect(Collectors.toList());

        //String tk = jwtUtil.createToken(u, roles);
        //System.out.println("Token generado:");
        //System.out.println(tk);
        return new MyUserPrincipal(u, roles);
    }
}
