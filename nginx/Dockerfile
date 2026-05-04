FROM nginx:1.27-alpine

ARG NGINX_CONF=nginx.dev.conf

COPY ${NGINX_CONF} /etc/nginx/conf.d/default.conf

RUN rm -f /etc/nginx/conf.d/default.conf.bak

EXPOSE 80