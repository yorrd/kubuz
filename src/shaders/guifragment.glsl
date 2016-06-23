#version 330

in vec2 outTexCoord;
out vec4 fragColor;

uniform sampler2D texture_diffuse;

void main() {
    fragColor = vec4(1.0, 1.0, 0, 1.0) * texture2D(texture_diffuse, outTexCoord);
}