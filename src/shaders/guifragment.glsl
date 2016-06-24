#version 330

in vec2 outTexCoord;
out vec4 fragColor;

uniform sampler2D texture_diffuse;

void main(void) {
    fragColor = texture(texture_diffuse, outTexCoord);
}